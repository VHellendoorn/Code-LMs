#ifndef _TIMING_H_
#define _TIMING_H_

#include <opencv/cv.h>

//#define CPU_FREQ 2100000000

//typedef unsigned long long tick_t;

//static __inline__ tick_t getCPUFreq() {
//    return CPU_FREQ;
//}

//static void getCPUTick(tick_t *c)
//{
//     tick_t a, d;
//     asm("cpuid");
//     asm volatile("rdtsc" : "=a" (a), "=d" (d));

//     *c = (a | (d << 32));
//}

typedef int64 tick_t;

static __inline__ tick_t getCPUFreq() {
    return (tick_t) cvGetTickFrequency();
}

static __inline__ void getCPUTick(tick_t *c)
{
    *c = cvGetTickCount();
}

#define PRINT_TIMING(label, init, final, del) printf("%s: %.3lf%s", label, (final-init)/getCPUFreq()/1000.0, (del));

#endif
