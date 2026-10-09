import { createApp, defineComponent, ref } from 'vue'

import '@/assets/MessageBox.css'

interface MessageBoxProps {
  msg: string
}

const MessageBox = defineComponent({
  props: {
    msg: {
      type: String,
      required: true,
    },
  },
  setup(props: MessageBoxProps) {
    return () => (
      <div id="msgbox-root">
        <p>{props.msg}</p>
      </div>
    )
  },
})

function showMsg(msg: string) {
  const div = document.createElement('div')
  document.body.appendChild(div)
  const app = createApp(MessageBox, {
    msg,
  })
  setTimeout(() => {
    app.unmount()
    document.body.removeChild(div)
  }, 6000)
  app.mount(div)
}

const ConfirmBox = defineComponent({
  props: {
    msg: { type: String, required: true },
    resolve: { type: Function, required: true },
  },
  setup(props) {
    const visible = ref(true)
    function close(val: boolean) {
      visible.value = false
      props.resolve(val)
    }
    return () =>
      visible.value ? (
        <div class="confirm-overlay" onClick={() => close(false)}>
          <div class="confirm-card" onClick={(e: Event) => e.stopPropagation()}>
            <p class="confirm-msg">{props.msg}</p>
            <div class="confirm-actions">
              <button class="confirm-btn confirm-btn--cancel" onClick={() => close(false)}>
                取消
              </button>
              <button class="confirm-btn confirm-btn--ok" onClick={() => close(true)}>
                确认
              </button>
            </div>
          </div>
        </div>
      ) : null
  },
})

function showConfirm(msg: string): Promise<boolean> {
  return new Promise((resolve) => {
    const div = document.createElement('div')
    document.body.appendChild(div)
    const app = createApp(ConfirmBox, {
      msg,
      resolve: (val: boolean) => {
        resolve(val)
        setTimeout(() => {
          app.unmount()
          document.body.removeChild(div)
        }, 200)
      },
    })
    app.mount(div)
  })
}

export { showMsg, showConfirm }
