// Copyright (c) 2011 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "net/base/keygen_handler.h"

#include <windows.h>
#include <rpc.h>

#include <list>
#include <string>
#include <vector>

#include "base/base64.h"
#include "base/basictypes.h"
#include "base/logging.h"
#include "base/strings/string_piece.h"
#include "base/strings/string_util.h"
#include "base/strings/utf_string_conversions.h"
#include "crypto/capi_util.h"
#include "crypto/scoped_capi_types.h"
#include "crypto/wincrypt_shim.h"

#pragma comment(lib, "crypt32.lib")
#pragma comment(lib, "rpcrt4.lib")

namespace net {

// Assigns the contents of a CERT_PUBLIC_KEY_INFO structure for the signing
// key in |prov| to |output|. Returns true if encoding was successful.
bool GetSubjectPublicKeyInfo(HCRYPTPROV prov, std::vector<BYTE>* output) {
  BOOL ok;
  DWORD size = 0;

  // From the private key stored in HCRYPTPROV, obtain the public key, stored
  // as a CERT_PUBLIC_KEY_INFO structure. Currently, only RSA public keys are
  // supported.
  ok = CryptExportPublicKeyInfoEx(prov, AT_KEYEXCHANGE, X509_ASN_ENCODING,
                                  const_cast<char*>(szOID_RSA_RSA), 0, NULL,
                                  NULL, &size);
  DCHECK(ok);
  if (!ok)
    return false;

  output->resize(size);

  PCERT_PUBLIC_KEY_INFO public_key_casted =
      reinterpret_cast<PCERT_PUBLIC_KEY_INFO>(&(*output)[0]);
  ok = CryptExportPublicKeyInfoEx(prov, AT_KEYEXCHANGE, X509_ASN_ENCODING,
                                  const_cast<char*>(szOID_RSA_RSA), 0, NULL,
                                  public_key_casted, &size);
  DCHECK(ok);
  if (!ok)
    return false;

  output->resize(size);

  return true;
}

// Generates a DER encoded SignedPublicKeyAndChallenge structure from the
// signing key of |prov| and the specified ASCII |challenge| string and
// appends it to |output|.
// True if the encoding was successfully generated.
bool GetSignedPublicKeyAndChallenge(HCRYPTPROV prov,
                                    const std::string& challenge,
                                    std::string* output) {
  base::string16 challenge16 = base::ASCIIToUTF16(challenge);
  std::vector<BYTE> spki;

  if (!GetSubjectPublicKeyInfo(prov, &spki))
    return false;

  // PublicKeyAndChallenge ::= SEQUENCE {
  //     spki SubjectPublicKeyInfo,
  //     challenge IA5STRING
  // }
  CERT_KEYGEN_REQUEST_INFO pkac;
  pkac.dwVersion = CERT_KEYGEN_REQUEST_V1;
  pkac.SubjectPublicKeyInfo =
      *reinterpret_cast<PCERT_PUBLIC_KEY_INFO>(&spki[0]);
  pkac.pwszChallengeString = const_cast<base::char16*>(challenge16.c_str());

  CRYPT_ALGORITHM_IDENTIFIER sig_alg;
  memset(&sig_alg, 0, sizeof(sig_alg));
  sig_alg.pszObjId = const_cast<char*>(szOID_RSA_MD5RSA);

  BOOL ok;
  DWORD size = 0;
  std::vector<BYTE> signed_pkac;
  ok = CryptSignAndEncodeCertificate(prov, AT_KEYEXCHANGE, X509_ASN_ENCODING,
                                     X509_KEYGEN_REQUEST_TO_BE_SIGNED,
                                     &pkac, &sig_alg, NULL,
                                     NULL, &size);
  DCHECK(ok);
  if (!ok)
    return false;

  signed_pkac.resize(size);
  ok = CryptSignAndEncodeCertificate(prov, AT_KEYEXCHANGE, X509_ASN_ENCODING,
                                     X509_KEYGEN_REQUEST_TO_BE_SIGNED,
                                     &pkac, &sig_alg, NULL,
                                     &signed_pkac[0], &size);
  DCHECK(ok);
  if (!ok)
    return false;

  output->assign(reinterpret_cast<char*>(&signed_pkac[0]), size);
  return true;
}

// Generates a unique name for the container which will store the key that is
// generated. The traditional Windows approach is to use a GUID here.
std::wstring GetNewKeyContainerId() {
  RPC_STATUS status = RPC_S_OK;
  std::wstring result;

  UUID id = { 0 };
  status = UuidCreateSequential(&id);
  if (status != RPC_S_OK && status != RPC_S_UUID_LOCAL_ONLY)
    return result;

  RPC_WSTR rpc_string = NULL;
  status = UuidToString(&id, &rpc_string);
  if (status != RPC_S_OK)
    return result;

  // RPC_WSTR is unsigned short*.  wchar_t is a built-in type of Visual C++,
  // so the type cast is necessary.
  result.assign(reinterpret_cast<wchar_t*>(rpc_string));
  RpcStringFree(&rpc_string);

  return result;
}

// This is a helper struct designed to optionally delete a key after releasing
// the associated provider.
struct KeyContainer {
 public:
  explicit KeyContainer(bool delete_keyset)
      : delete_keyset_(delete_keyset) {}

  ~KeyContainer() {
    if (provider_) {
      provider_.reset();
      if (delete_keyset_ && !key_id_.empty()) {
        HCRYPTPROV provider;
        crypto::CryptAcquireContextLocked(&provider, key_id_.c_str(), NULL,
            PROV_RSA_FULL, CRYPT_SILENT | CRYPT_DELETEKEYSET);
      }
    }
  }

  crypto::ScopedHCRYPTPROV provider_;
  std::wstring key_id_;

 private:
  bool delete_keyset_;
};

std::string KeygenHandler::GenKeyAndSignChallenge() {
  KeyContainer key_container(!stores_key_);

  // TODO(rsleevi): Have the user choose which provider they should use, which
  // needs to be filtered by those providers which can provide the key type
  // requested or the key size requested. This is especially important for
  // generating certificates that will be stored on smart cards.
  const int kMaxAttempts = 5;
  int attempt;
  for (attempt = 0; attempt < kMaxAttempts; ++attempt) {
    // Per MSDN documentation for CryptAcquireContext, if applications will be
    // creating their own keys, they should ensure unique naming schemes to
    // prevent overlap with any other applications or consumers of CSPs, and
    // *should not* store new keys within the default, NULL key container.
    key_container.key_id_ = GetNewKeyContainerId();
    if (key_container.key_id_.empty())
      return std::string();

    // Only create new key containers, so that existing key containers are not
    // overwritten.
    if (crypto::CryptAcquireContextLocked(key_container.provider_.receive(),
            key_container.key_id_.c_str(), NULL, PROV_RSA_FULL,
            CRYPT_SILENT | CRYPT_NEWKEYSET))
      break;

    if (GetLastError() != NTE_BAD_KEYSET) {
      LOG(ERROR) << "Keygen failed: Couldn't acquire a CryptoAPI provider "
                    "context: " << GetLastError();
      return std::string();
    }
  }
  if (attempt == kMaxAttempts) {
    LOG(ERROR) << "Keygen failed: Couldn't acquire a CryptoAPI provider "
                  "context: Max retries exceeded";
    return std::string();
  }

  {
    crypto::ScopedHCRYPTKEY key;
    if (!CryptGenKey(key_container.provider_, CALG_RSA_KEYX,
        (key_size_in_bits_ << 16) | CRYPT_EXPORTABLE, key.receive())) {
      LOG(ERROR) << "Keygen failed: Couldn't generate an RSA key";
      return std::string();
    }

    std::string spkac;
    if (!GetSignedPublicKeyAndChallenge(key_container.provider_, challenge_,
                                        &spkac)) {
      LOG(ERROR) << "Keygen failed: Couldn't generate the signed public key "
                    "and challenge";
      return std::string();
    }

    std::string result;
    base::Base64Encode(spkac, &result);

    VLOG(1) << "Keygen succeeded";
    return result;
  }
}

}  // namespace net
