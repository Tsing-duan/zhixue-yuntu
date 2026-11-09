#!/usr/bin/env python3
"""Disposable loopback-only acceptance services. Never imports into an existing database."""
import argparse, hashlib, json, os, secrets, shutil, socket, subprocess, time, urllib.request
from pathlib import Path
ROOT = Path(__file__).resolve().parents[1]
RUN = ROOT / '.run/modernization'
JAVA = Path('/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home/bin/java')
PORTS = {'mysql': 13306, 'redis': 16379, 'backend': 18080}

def free(port):
    with socket.socket() as s:
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        try: s.bind(('127.0.0.1', port))
        except OSError: raise SystemExit(f'Refusing occupied port {port}; no existing process was stopped.')

def start(name, args, env=None, cwd=None):
    free(PORTS[name])
    log = open(RUN / f'{name}.log', 'ab')
    p = subprocess.Popen([str(x) for x in args], cwd=cwd or RUN, env=env, stdout=log, stderr=log, start_new_session=True)
    (RUN / f'{name}.pid').write_text(str(p.pid))
    for _ in range(240):
        if p.poll() is not None: raise SystemExit(f'{name} exited: inspect {RUN / (name + ".log")}')
        try:
            with socket.create_connection(('127.0.0.1', PORTS[name]), timeout=.2): return
        except OSError: time.sleep(.25)
    raise SystemExit(f'{name} did not become ready')

def sql(query, database=None):
    args = ['mysql','--no-defaults','--protocol=TCP','-h127.0.0.1','-P13306','-uroot','-N','-B']
    if database: args += [database]
    return subprocess.check_output(args, input=query, text=True)

parser = argparse.ArgumentParser()
parser.add_argument('action', choices=['start', 'backend', 'stop'])
args = parser.parse_args()
RUN.mkdir(parents=True, exist_ok=True)
if args.action == 'stop':
    for name in ['backend','redis','mysql']:
        file = RUN / f'{name}.pid'
        if file.exists():
            pid = int(file.read_text())
            command = subprocess.run(['ps','-p',str(pid),'-o','command='],capture_output=True,text=True).stdout
            cwd = subprocess.run(['lsof','-a','-p',str(pid),'-d','cwd','-Fn'],capture_output=True,text=True).stdout
            owned = str(RUN) in command or any(line == 'n'+str(RUN) or line == 'n'+str(RUN/'runtime') for line in cwd.splitlines())
            if command and not owned:
                print(f'Refused to stop unrecognized PID for {name}'); continue
            if command:
                os.kill(pid, 15)
                for _ in range(100):
                    try: os.kill(pid, 0)
                    except ProcessLookupError: break
                    time.sleep(.1)
                else:
                    print(f'Test {name} is still stopping; kept PID for a later retry'); continue
                print(f'Stopped test {name}')
            file.unlink()
    raise SystemExit()
if args.action == 'start':
    for port in PORTS.values(): free(port)
    datadir = RUN / 'mysql'
    if not (datadir / 'mysql').exists():
        datadir.mkdir(exist_ok=True)
        with open(RUN/'mysql-init.log','w') as log:
            subprocess.run(['mysqld','--no-defaults','--initialize-insecure',f'--datadir={datadir}'],stdout=log,stderr=log,check=True)
    start('mysql',['mysqld','--no-defaults',f'--datadir={datadir}','--bind-address=127.0.0.1','--port=13306','--mysqlx=OFF',f'--socket=/tmp/zhixue-{hashlib.sha256(str(RUN).encode()).hexdigest()[:10]}.sock',f'--pid-file={RUN / "mysql-server.pid"}'])
    for attempt in range(100):
        ready = subprocess.run(['mysql','--no-defaults','--protocol=TCP','-h127.0.0.1','-P13306','-uroot','-e','SELECT 1'], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)
        if ready.returncode == 0: break
        time.sleep(.2)
    else: raise SystemExit('MySQL handshake did not become ready')
    db = 'zhixue_migration_' + time.strftime('%Y%m%d_%H%M%S') + '_' + secrets.token_hex(3)
    if sql(f"SELECT SCHEMA_NAME FROM INFORMATION_SCHEMA.SCHEMATA WHERE SCHEMA_NAME='{db}'").strip():
        raise SystemExit('Refusing existing database')
    sql(f'CREATE DATABASE `{db}` CHARACTER SET utf8mb4')
    for file in ['portfolio-demo.sql','migrate-ai-data-agent.sql']:
        content = (ROOT/'sql'/file).read_text()
        if any(line.strip().upper().startswith(('USE ', 'CREATE DATABASE')) for line in content.splitlines()):
            raise SystemExit('Refusing SQL that changes database context')
        sql(content, db)
    start('redis',['redis-server','--bind','127.0.0.1','--port','16379','--save','','--appendonly','no','--dir',str(RUN)])
    runtime = RUN / 'runtime'
    runtime.mkdir(exist_ok=True)
    shutil.copytree(ROOT/'learn_server-master/img', runtime/'img', dirs_exist_ok=True)
    state = {'database':db,'jwt':secrets.token_hex(32),'ports':PORTS}
    statefile = RUN/'state.json'
    statefile.write_text(json.dumps(state))
    statefile.chmod(0o600)
    print(f'Created isolated database {db}; dedicated Redis 16379; uploads under {runtime}')
else:
    state = json.loads((RUN/'state.json').read_text())
    env = dict(os.environ, DB_PASSWORD='', JWT_SECRET=state['jwt'], DEEPSEEK_API_KEY='', DEEPSEEK_BASE_URL='https://api.deepseek.com', APE_CORS_ALLOWED_ORIGINS=','.join(f'http://{host}:{port}' for host in ['127.0.0.1','localhost'] for port in [3000,3001,14000,14001,15000,15001]))
    jar = ROOT/'learn_server-master/ape-admin/target/ape-admin-0.0.1-SNAPSHOT.jar'
    start('backend',[JAVA,'-jar',jar,'--server.address=127.0.0.1','--server.port=18080',f'--spring.datasource.druid.master.url=jdbc:mysql://127.0.0.1:13306/{state["database"]}?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true','--spring.redis.host=127.0.0.1','--spring.redis.port=16379','--spring.redis.database=0',f'--logging.file.path={RUN / "runtime/logs"}'],env,RUN/'runtime')
    for attempt in range(120):
        try:
            with urllib.request.urlopen('http://127.0.0.1:18080/school/getApeSchoolList', timeout=1) as response:
                if response.status == 200: break
        except Exception: time.sleep(.25)
    else: raise SystemExit('Backend HTTP readiness failed; inspect backend.log')
    print('Isolated backend ready on 18080; real AI provider disabled')
