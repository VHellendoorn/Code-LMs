// Copyright 2013 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "net/quic/test_tools/crypto_test_utils.h"

#include <openssl/bn.h>
#include <openssl/ec.h>
#include <openssl/ecdsa.h>
#include <openssl/evp.h>
#include <openssl/obj_mac.h>
#include <openssl/sha.h>

#include "crypto/openssl_util.h"
#include "crypto/scoped_openssl_types.h"
#include "crypto/secure_hash.h"
#include "net/quic/crypto/channel_id.h"

using base::StringPiece;
using std::string;

namespace net {

namespace test {

class TestChannelIDKey : public ChannelIDKey {
 public:
  explicit TestChannelIDKey(EVP_PKEY* ecdsa_key) : ecdsa_key_(ecdsa_key) {}
  ~TestChannelIDKey() override {}

  // ChannelIDKey implementation.

  bool Sign(StringPiece signed_data, string* out_signature) const override {
    crypto::ScopedEVP_MD_CTX md_ctx(EVP_MD_CTX_create());
    if (!md_ctx ||
        EVP_DigestSignInit(md_ctx.get(), nullptr, EVP_sha256(), nullptr,
                           ecdsa_key_.get()) != 1) {
      return false;
    }

    EVP_DigestUpdate(md_ctx.get(), ChannelIDVerifier::kContextStr,
                     strlen(ChannelIDVerifier::kContextStr) + 1);
    EVP_DigestUpdate(md_ctx.get(), ChannelIDVerifier::kClientToServerStr,
                     strlen(ChannelIDVerifier::kClientToServerStr) + 1);
    EVP_DigestUpdate(md_ctx.get(), signed_data.data(), signed_data.size());

    size_t sig_len;
    if (!EVP_DigestSignFinal(md_ctx.get(), nullptr, &sig_len)) {
      return false;
    }

    scoped_ptr<uint8[]> der_sig(new uint8[sig_len]);
    if (!EVP_DigestSignFinal(md_ctx.get(), der_sig.get(), &sig_len)) {
      return false;
    }

    uint8* derp = der_sig.get();
    crypto::ScopedECDSA_SIG sig(
        d2i_ECDSA_SIG(nullptr, const_cast<const uint8**>(&derp), sig_len));
    if (sig.get() == nullptr) {
      return false;
    }

    // The signature consists of a pair of 32-byte numbers.
    static const size_t kSignatureLength = 32 * 2;
    scoped_ptr<uint8[]> signature(new uint8[kSignatureLength]);
    if (!BN_bn2bin_padded(&signature[0], 32, sig->r) ||
        !BN_bn2bin_padded(&signature[32], 32, sig->s)) {
      return false;
    }

    *out_signature = string(reinterpret_cast<char*>(signature.get()),
                            kSignatureLength);

    return true;
  }

  string SerializeKey() const override {
    // i2d_PublicKey will produce an ANSI X9.62 public key which, for a P-256
    // key, is 0x04 (meaning uncompressed) followed by the x and y field
    // elements as 32-byte, big-endian numbers.
    static const int kExpectedKeyLength = 65;

    int len = i2d_PublicKey(ecdsa_key_.get(), nullptr);
    if (len != kExpectedKeyLength) {
      return "";
    }

    uint8 buf[kExpectedKeyLength];
    uint8* derp = buf;
    i2d_PublicKey(ecdsa_key_.get(), &derp);

    return string(reinterpret_cast<char*>(buf + 1), kExpectedKeyLength - 1);
  }

 private:
  crypto::ScopedEVP_PKEY ecdsa_key_;
};

class TestChannelIDSource : public ChannelIDSource {
 public:
  ~TestChannelIDSource() override {}

  // ChannelIDSource implementation.

  QuicAsyncStatus GetChannelIDKey(
      const string& hostname,
      scoped_ptr<ChannelIDKey>* channel_id_key,
      ChannelIDSourceCallback* /*callback*/) override {
    channel_id_key->reset(new TestChannelIDKey(HostnameToKey(hostname)));
    return QUIC_SUCCESS;
  }

 private:
  static EVP_PKEY* HostnameToKey(const string& hostname) {
    // In order to generate a deterministic key for a given hostname the
    // hostname is hashed with SHA-256 and the resulting digest is treated as a
    // big-endian number. The most-significant bit is cleared to ensure that
    // the resulting value is less than the order of the group and then it's
    // taken as a private key. Given the private key, the public key is
    // calculated with a group multiplication.
    SHA256_CTX sha256;
    SHA256_Init(&sha256);
    SHA256_Update(&sha256, hostname.data(), hostname.size());

    unsigned char digest[SHA256_DIGEST_LENGTH];
    SHA256_Final(digest, &sha256);

    // Ensure that the digest is less than the order of the P-256 group by
    // clearing the most-significant bit.
    digest[0] &= 0x7f;

    crypto::ScopedBIGNUM k(BN_new());
    CHECK(BN_bin2bn(digest, sizeof(digest), k.get()) != nullptr);

    crypto::ScopedOpenSSL<EC_GROUP, EC_GROUP_free>::Type p256(
        EC_GROUP_new_by_curve_name(NID_X9_62_prime256v1));
    CHECK(p256.get());

    crypto::ScopedEC_KEY ecdsa_key(EC_KEY_new());
    CHECK(ecdsa_key.get() != nullptr &&
          EC_KEY_set_group(ecdsa_key.get(), p256.get()));

    crypto::ScopedOpenSSL<EC_POINT, EC_POINT_free>::Type point(
        EC_POINT_new(p256.get()));
    CHECK(EC_POINT_mul(p256.get(), point.get(), k.get(), nullptr, nullptr,
                       nullptr));

    EC_KEY_set_private_key(ecdsa_key.get(), k.get());
    EC_KEY_set_public_key(ecdsa_key.get(), point.get());

    crypto::ScopedEVP_PKEY pkey(EVP_PKEY_new());
    // EVP_PKEY_set1_EC_KEY takes a reference so no |release| here.
    EVP_PKEY_set1_EC_KEY(pkey.get(), ecdsa_key.get());

    return pkey.release();
  }
};

// static
ChannelIDSource* CryptoTestUtils::ChannelIDSourceForTesting() {
  return new TestChannelIDSource();
}

}  // namespace test

}  // namespace net
