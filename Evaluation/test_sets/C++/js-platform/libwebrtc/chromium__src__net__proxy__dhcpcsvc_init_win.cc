// Copyright (c) 2011 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "net/proxy/dhcpcsvc_init_win.h"

#include "base/lazy_instance.h"
#include "base/logging.h"

#include <dhcpcsdk.h>
#include <dhcpv6csdk.h>

namespace {

class DhcpcsvcInitSingleton {
 public:
  DhcpcsvcInitSingleton() {
    DWORD version = 0;
    DWORD err = DhcpCApiInitialize(&version);
    DCHECK(err == ERROR_SUCCESS);  // DCHECK_EQ complains of unsigned mismatch.
  }

  ~DhcpcsvcInitSingleton() {
    // Worker pool threads that use the DHCP API may still be running, so skip
    // cleanup.
  }
};

static base::LazyInstance<DhcpcsvcInitSingleton> g_dhcpcsvc_init_singleton =
    LAZY_INSTANCE_INITIALIZER;

}  // namespace

namespace net {

void EnsureDhcpcsvcInit() {
  g_dhcpcsvc_init_singleton.Get();
}

}  // namespace net
