// @vitest-environment jsdom
import { it, expect, vi } from 'vitest'
import { createApp, defineComponent, h } from 'vue'
import { componentBus, releaseBus, installEventBus } from './eventBus'
it('cleans only subscriptions owned by an unmounted component', () => {
 const a = {} as any, b = {} as any; const one = vi.fn(), two = vi.fn()
 componentBus(a).$on('collapse',one); componentBus(b).$on('collapse',two)
 releaseBus(a); componentBus(b).$emit('collapse',true)
 expect(one).not.toHaveBeenCalled(); expect(two).toHaveBeenCalledExactlyOnceWith(true)
 releaseBus(b); componentBus(b).$emit('collapse',false); expect(two).toHaveBeenCalledTimes(1)
})

it('binds subscriptions to the actual Options API component lifecycle', () => {
 const received=vi.fn()
 const receiver=createApp(defineComponent({created(){this.$bus.$on('lifecycle-check',received)},render(){return h('div')}}))
 installEventBus(receiver)
 const element=document.createElement('div');document.body.append(element);receiver.mount(element)
 const sender=componentBus({} as any)
 sender.$emit('lifecycle-check',1);expect(received).toHaveBeenCalledExactlyOnceWith(1)
 receiver.unmount();sender.$emit('lifecycle-check',2);expect(received).toHaveBeenCalledTimes(1)
 element.remove()
})
