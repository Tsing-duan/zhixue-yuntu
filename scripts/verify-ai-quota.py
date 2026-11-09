"""Exercise the actual quota Lua against isolated Redis only; remove own keys."""
import concurrent.futures, json, pathlib, subprocess, uuid
root = pathlib.Path(__file__).resolve().parents[1]
state = json.loads((root / '.run/modernization/state.json').read_text())
assert state['ports']['redis'] == 16379 and state['database'].startswith('zhixue_migration_')
base = ['redis-cli', '-h', '127.0.0.1', '-p', '16379', '--raw']
prefix = 'verification:ai-budget:{' + uuid.uuid4().hex + '}:'
keys = [prefix + suffix for suffix in ['daily', 'flight', 'cooldown']]
reserve = (root / 'learn_server-master/ape-admin/src/main/resources/ai/reserve-budget.lua').read_text()
complete = (root / 'learn_server-master/ape-admin/src/main/resources/ai/complete-budget.lua').read_text()
def redis(*args):
    return subprocess.check_output(base + list(map(str, args)), text=True).strip()
def acquire(owner='owner', calls=30, tokens=20000, reserved=6000, history_calls=0, history_tokens=0):
    return int(redis('EVAL', reserve, 3, *keys, owner, 60, 15, 3600, calls, tokens, reserved, history_calls, history_tokens))
def finish(owner='owner', reserved=6000, actual=20, outcome='success'):
    return int(redis('EVAL', complete, 3, *keys, owner, reserved, actual, outcome))
def ledger():
    values = redis('HGETALL', keys[0]).splitlines()
    return dict(zip(values[::2], map(int, values[1::2])))
checks=[]
try:
    with concurrent.futures.ThreadPoolExecutor(max_workers=10) as pool:
        results=list(pool.map(lambda _: acquire(),range(10)))
    assert results.count(0)==1 and results.count(1)==9
    checks.append('10 concurrent admissions: exactly 1 accepted')
    assert finish('wrong-owner')==0 and redis('GET',keys[1])=='owner'
    assert finish()==1 and ledger()=={'calls':1,'tokens':20}
    assert acquire()==2
    checks.append('owner-safe release, success usage reconciliation, shared cooldown')
    redis('DEL',keys[2]); assert acquire(history_calls=0)==0
    finish(outcome='failure'); assert ledger()=={'calls':2,'tokens':6020}
    checks.append('failure retains reservation; empty/deleted history does not reset ledger')
    redis('DEL',keys[2]); assert acquire()==0
    finish(outcome='not-configured'); assert ledger()=={'calls':2,'tokens':6020}
    checks.append('disabled/unconfigured refunds admission without success history')
    redis('DEL',keys[2]); assert acquire(calls=2)==3
    assert acquire(tokens=7000)==4
    assert acquire(history_calls=30)==3
    assert acquire(history_tokens=19000)==4
    checks.append('daily calls/tokens and persisted history floor enforced')
    assert acquire()==0
    redis('DEL',keys[1],keys[2]); assert acquire(owner='replacement')==0
    before=ledger(); assert finish()==0 and ledger()==before and redis('GET',keys[1])=='replacement'
    checks.append('expired lease cannot release or refund replacement request')
finally:
    redis('DEL',*keys)
out=root/'output/playwright/ai-hardening';out.mkdir(parents=True,exist_ok=True)
(out/'redis-quota-verification.json').write_text(json.dumps({'checks':checks,'isolatedPort':16379,'testKeysRemoved':True},ensure_ascii=False,indent=2))
print('Passed',len(checks),'Redis quota scenarios')
