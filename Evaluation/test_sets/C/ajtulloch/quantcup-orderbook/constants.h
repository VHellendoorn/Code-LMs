#pragma once

#include "types.h"

#include <stdint.h>
#include <limits>

// Define it here.
typedef unsigned short t_price;

namespace OB {

constexpr t_price kMaxPrice = std::numeric_limits<t_price>::max();

constexpr int kMaxNumOrders = 101000;

constexpr t_price kMinPrice = 1;

constexpr uint32_t kMaxLiveOrders = std::numeric_limits<t_price>::max();

constexpr size_t kFieldLength = 4;
}
