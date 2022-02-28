#include "vcpu.h"
#include "schedule.h"

static void do_nothing(void) {}
static void *return_nothing(void){ return NULL; }

schedule_operation_t default_schedule = {
    do_nothing,
    (T_VCPU *(*)(void))do_nothing,
    do_nothing,
    (void (*)(T_VCPU *))do_nothing,
    (void (*)(T_VCPU *))do_nothing,
    (void *(*)(T_VCPU *))return_nothing,
    (void (*)(T_VCPU *))do_nothing,
};
