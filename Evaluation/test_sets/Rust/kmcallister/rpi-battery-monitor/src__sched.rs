use libc;
use errno;

/// Priority to use in realtime mode.
const RT_PRIO: libc::c_int = 50;

fn set_scheduler(policy: libc::c_int, prio: libc::c_int) {
    let param = libc::sched_param {
        sched_priority: prio,
    };

    unsafe {
        assert_eq!(0, libc::sched_setscheduler(0, policy, &param));
    }
}

/// RAII helper for entering/exiting realtime scheduler priority.
pub struct Realtime {
    orig_policy: libc::c_int,
    orig_prio: libc::c_int,
}

impl Realtime {
    /// Enter a realtime scheduler context.
    ///
    /// Returns to the original scheduler on drop.
    pub fn enter() -> Realtime {
        let policy = unsafe {
            libc::sched_getscheduler(0)
        };
        assert!(policy >= 0);

        // stupid errno dance
        errno::set_errno(errno::Errno(0));
        let prio = unsafe {
            libc::getpriority(libc::PRIO_PROCESS as u32, 0)
        };
        assert_eq!(0, errno::errno().0);

        set_scheduler(libc::SCHED_FIFO, RT_PRIO);

        Realtime {
            orig_policy: policy,
            orig_prio: prio,
        }
    }
}

impl Drop for Realtime {
    fn drop(&mut self) {
        set_scheduler(self.orig_policy, self.orig_prio);
    }
}
