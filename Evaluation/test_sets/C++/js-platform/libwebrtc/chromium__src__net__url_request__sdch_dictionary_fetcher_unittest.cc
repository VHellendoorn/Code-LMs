// Copyright 2014 The Chromium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

#include "net/url_request/sdch_dictionary_fetcher.h"

#include <string>

#include "base/bind.h"
#include "base/run_loop.h"
#include "base/strings/stringprintf.h"
#include "base/thread_task_runner_handle.h"
#include "net/base/sdch_manager.h"
#include "net/url_request/url_request_data_job.h"
#include "net/url_request/url_request_filter.h"
#include "net/url_request/url_request_interceptor.h"
#include "net/url_request/url_request_test_util.h"
#include "testing/gtest/include/gtest/gtest.h"
#include "url/gurl.h"

namespace net {

static const char* kSampleBufferContext = "This is a sample buffer.";
static const char* kTestDomain = "top.domain.test";

class URLRequestSpecifiedResponseJob : public URLRequestSimpleJob {
 public:
  URLRequestSpecifiedResponseJob(URLRequest* request,
                                 NetworkDelegate* network_delegate)
      : URLRequestSimpleJob(request, network_delegate) {}

  static void AddUrlHandler() {
    net::URLRequestFilter* filter = net::URLRequestFilter::GetInstance();
    jobs_requested_ = 0;
    filter->AddHostnameHandler(
        "http", kTestDomain, &URLRequestSpecifiedResponseJob::Factory);
  }

  static void RemoveUrlHandler() {
    net::URLRequestFilter* filter = net::URLRequestFilter::GetInstance();
    filter->RemoveHostnameHandler("http", kTestDomain);
    jobs_requested_ = 0;
  }

  static URLRequestJob* Factory(URLRequest* request,
                                net::NetworkDelegate* network_delegate,
                                const std::string& scheme) {
    ++jobs_requested_;
    return new URLRequestSpecifiedResponseJob(request, network_delegate);
  }

  static std::string ExpectedResponseForURL(const GURL& url) {
    return base::StringPrintf("Response for %s\n%s\nEnd Response for %s\n",
                              url.spec().c_str(),
                              kSampleBufferContext,
                              url.spec().c_str());
  }

  static int jobs_requested() { return jobs_requested_; }

 private:
  ~URLRequestSpecifiedResponseJob() override{};
  int GetData(std::string* mime_type,
              std::string* charset,
              std::string* data,
              const CompletionCallback& callback) const override {
    GURL url(request_->url());
    *data = ExpectedResponseForURL(url);
    return OK;
  }

  static int jobs_requested_;
};

int URLRequestSpecifiedResponseJob::jobs_requested_(0);

class SdchDictionaryFetcherTest : public ::testing::Test {
 public:
  struct DictionaryAdditions {
    DictionaryAdditions(const std::string& dictionary_text,
                        const GURL& dictionary_url)
        : dictionary_text(dictionary_text), dictionary_url(dictionary_url) {}

    std::string dictionary_text;
    GURL dictionary_url;
  };

  SdchDictionaryFetcherTest() {
    URLRequestSpecifiedResponseJob::AddUrlHandler();
    context_.reset(new TestURLRequestContext);
    fetcher_.reset(new SdchDictionaryFetcher(
        context_.get(),
        base::Bind(&SdchDictionaryFetcherTest::OnDictionaryFetched,
                   base::Unretained(this))));
  }

  ~SdchDictionaryFetcherTest() {
    URLRequestSpecifiedResponseJob::RemoveUrlHandler();
  }

  void OnDictionaryFetched(const std::string& dictionary_text,
                           const GURL& dictionary_url,
                           const BoundNetLog& net_log) {
    dictionary_additions.push_back(
        DictionaryAdditions(dictionary_text, dictionary_url));
  }

  void GetDictionaryAdditions(std::vector<DictionaryAdditions>* out) {
    out->swap(dictionary_additions);
    dictionary_additions.clear();
  }

  SdchDictionaryFetcher* fetcher() { return fetcher_.get(); }

  // May not be called outside the SetUp()/TearDown() interval.
  int JobsRequested() {
    return URLRequestSpecifiedResponseJob::jobs_requested();
  }

  GURL PathToGurl(const char* path) {
    std::string gurl_string("http://");
    gurl_string += kTestDomain;
    gurl_string += "/";
    gurl_string += path;
    return GURL(gurl_string);
  }

 private:
  scoped_ptr<TestURLRequestContext> context_;
  scoped_ptr<SdchDictionaryFetcher> fetcher_;
  std::vector<DictionaryAdditions> dictionary_additions;
};

// Schedule a fetch and make sure it happens.
TEST_F(SdchDictionaryFetcherTest, Basic) {
  GURL dictionary_url(PathToGurl("dictionary"));
  fetcher()->Schedule(dictionary_url);

  base::RunLoop().RunUntilIdle();
  EXPECT_EQ(1, JobsRequested());
  std::vector<DictionaryAdditions> additions;
  GetDictionaryAdditions(&additions);
  ASSERT_EQ(1u, additions.size());
  EXPECT_EQ(
      URLRequestSpecifiedResponseJob::ExpectedResponseForURL(dictionary_url),
      additions[0].dictionary_text);
}

// Multiple fetches of the same URL should result in only one request.
TEST_F(SdchDictionaryFetcherTest, Multiple) {
  GURL dictionary_url(PathToGurl("dictionary"));
  fetcher()->Schedule(dictionary_url);
  fetcher()->Schedule(dictionary_url);
  fetcher()->Schedule(dictionary_url);
  base::RunLoop().RunUntilIdle();

  EXPECT_EQ(1, JobsRequested());
  std::vector<DictionaryAdditions> additions;
  GetDictionaryAdditions(&additions);
  ASSERT_EQ(1u, additions.size());
  EXPECT_EQ(
      URLRequestSpecifiedResponseJob::ExpectedResponseForURL(dictionary_url),
      additions[0].dictionary_text);
}

// A cancel should result in no actual requests being generated.
TEST_F(SdchDictionaryFetcherTest, Cancel) {
  GURL dictionary_url_1(PathToGurl("dictionary_1"));
  GURL dictionary_url_2(PathToGurl("dictionary_2"));
  GURL dictionary_url_3(PathToGurl("dictionary_3"));

  fetcher()->Schedule(dictionary_url_1);
  fetcher()->Schedule(dictionary_url_2);
  fetcher()->Schedule(dictionary_url_3);
  fetcher()->Cancel();
  base::RunLoop().RunUntilIdle();

  // Synchronous execution may have resulted in a single job being scheduled.
  EXPECT_GE(1, JobsRequested());
}
}
