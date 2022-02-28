/*
 * libjingle
 * Copyright 2004 Google Inc.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#ifndef WEBRTC_LIBJINGLE_SESSION_MEDIA_MEDIASESSIONCLIENT_H_
#define WEBRTC_LIBJINGLE_SESSION_MEDIA_MEDIASESSIONCLIENT_H_

#include <algorithm>
#include <map>
#include <string>
#include <vector>

#include "talk/media/base/cryptoparams.h"
#include "talk/session/media/channelmanager.h"
#include "talk/session/media/mediasession.h"
#include "webrtc/base/messagequeue.h"
#include "webrtc/base/sigslot.h"
#include "webrtc/base/sigslotrepeater.h"
#include "webrtc/base/thread.h"
#include "webrtc/libjingle/session/media/call.h"
#include "webrtc/libjingle/session/sessionmanager.h"
#include "webrtc/p2p/base/sessionclient.h"
#include "webrtc/p2p/base/sessiondescription.h"

namespace cricket {

class Call;

class MediaSessionClient : public SessionClient, public sigslot::has_slots<> {
 public:
#if !defined(DISABLE_MEDIA_ENGINE_FACTORY)
  MediaSessionClient(const buzz::Jid& jid, SessionManager *manager);
#endif
  // Alternative constructor, allowing injection of media_engine
  // and device_manager.
  MediaSessionClient(const buzz::Jid& jid, SessionManager *manager,
                     MediaEngineInterface* media_engine,
                     DataEngineInterface* data_media_engine,
                     DeviceManagerInterface* device_manager);
  ~MediaSessionClient();

  const buzz::Jid &jid() const { return jid_; }
  SessionManager* session_manager() const { return session_manager_; }
  ChannelManager* channel_manager() const { return channel_manager_; }

  // Return mapping of call ids to Calls.
  const std::map<uint32, Call *>& calls() const { return calls_; }

  // The settings below combine with the settings on SessionManager to choose

  // whether SDES-SRTP, DTLS-SRTP, or no security should be used. The possible
  // combinations are shown in the following table. Note that where either DTLS
  // or SDES is possible, DTLS is preferred. Thus to require either SDES or
  // DTLS, but not mandate DTLS, set SDES to require and DTLS to enable.
  //
  //              | SDES:Disable   | SDES:Enable    | SDES:Require   |
  // ----------------------------------------------------------------|
  // DTLS:Disable | No SRTP        | SDES Optional  | SDES Mandatory |
  // DTLS:Enable  | DTLS Optional  | DTLS/SDES Opt  | DTLS/SDES Mand |
  // DTLS:Require | DTLS Mandatory | DTLS Mandatory | DTLS Mandatory |

  // Control use of SDES-SRTP.
  SecurePolicy secure() const { return desc_factory_.secure(); }
  void set_secure(SecurePolicy s) { desc_factory_.set_secure(s); }

  // Control use of multiple sessions in a call.
  void set_multisession_enabled(bool multisession_enabled) {
    multisession_enabled_ = multisession_enabled;
  }

  int GetCapabilities() { return channel_manager_->GetCapabilities(); }

  Call *CreateCall();
  void DestroyCall(Call *call);

  Call *GetFocus();
  void SetFocus(Call *call);

  void JoinCalls(Call *call_to_join, Call *call);

  bool GetAudioInputDevices(std::vector<std::string>* names) {
    return channel_manager_->GetAudioInputDevices(names);
  }
  bool GetAudioOutputDevices(std::vector<std::string>* names) {
    return channel_manager_->GetAudioOutputDevices(names);
  }
  bool GetVideoCaptureDevices(std::vector<std::string>* names) {
    return channel_manager_->GetVideoCaptureDevices(names);
  }

  bool SetAudioOptions(const std::string& in_name, const std::string& out_name,
                       const AudioOptions& options) {
    return channel_manager_->SetAudioOptions(in_name, out_name, options);
  }
  bool SetOutputVolume(int level) {
    return channel_manager_->SetOutputVolume(level);
  }
  bool SetCaptureDevice(const std::string& cam_device) {
    return channel_manager_->SetCaptureDevice(cam_device);
  }

  SessionDescription* CreateOffer(const CallOptions& options) {
    return desc_factory_.CreateOffer(options, NULL);
  }
  SessionDescription* CreateAnswer(const SessionDescription* offer,
                                   const CallOptions& options) {
    return desc_factory_.CreateAnswer(offer, options, NULL);
  }

  sigslot::signal2<Call *, Call *> SignalFocus;
  sigslot::signal1<Call *> SignalCallCreate;
  sigslot::signal1<Call *> SignalCallDestroy;
  sigslot::repeater0<> SignalDevicesChange;

  virtual bool ParseContent(SignalingProtocol protocol,
                            const buzz::XmlElement* elem,
                            ContentDescription** content,
                            ParseError* error);
  virtual bool IsWritable(SignalingProtocol protocol,
                          const ContentDescription* content);
  virtual bool WriteContent(SignalingProtocol protocol,
                            const ContentDescription* content,
                            buzz::XmlElement** elem,
                            WriteError* error);

 private:
  void Construct();
  void OnSessionCreate(Session *session, bool received_initiate);
  void OnSessionState(BaseSession *session, BaseSession::State state);
  void OnSessionDestroy(Session *session);
  Session *CreateSession(Call *call);
  Session *CreateSession(const std::string& id, Call* call);
  Call *FindCallByRemoteName(const std::string &remote_name);

  buzz::Jid jid_;
  SessionManager* session_manager_;
  Call *focus_call_;
  ChannelManager *channel_manager_;
  MediaSessionDescriptionFactory desc_factory_;
  bool multisession_enabled_;
  std::map<uint32, Call *> calls_;

  // Maintain a mapping of session id to call.
  typedef std::map<std::string, Call *> SessionMap;
  SessionMap session_map_;

  friend class Call;
};

}  // namespace cricket

#endif  // WEBRTC_LIBJINGLE_SESSION_MEDIA_MEDIASESSIONCLIENT_H_
