require 'spec_helper'

# Intercept the hook that sensu uses to auto-execute checks by entirely replacing
# the method used in Kernel before loading the handler.
# This is _terrible_, see also https://github.com/sensu/sensu-plugin/pull/61
module Kernel
  def at_exit(&block)
  end
end

module Sensu
  class Handler
    attr_accessor :event
  end
end

require "#{File.dirname(__FILE__)}/../../files/mailer"

class Mailer
  attr_accessor :settings
end

describe Mailer do
  include SensuHandlerTestHelper

  subject { Mailer.new }
  before(:each) do
      setup_event! do |e|
        e['check']['status'] = 2
        e['check']['page'] = true
        e['check']['runbook'] = 'http://some.runbook/'
        e['check']['team'] = 'someotherteam'
      end
  end

  it "Doesn't work without a mail destination" do
    setup_event!
    expect(subject).to receive(:bail).and_return(nil).once
    expect(subject).not_to receive(:create_issue)
    subject.handle
  end

  it "properly reads the email from the check" do
    subject.event['check']['notification_email'] = "test@example.com"
    subject.event['check']['status'] = 2
    subject.event['check']['name'] = 'fake_alert'
    subject.event['client']['name'] = 'fake_client'
    expect(Mail).to receive(:deliver)
    expect(subject).not_to receive(:bail)
    subject.stub(:log).and_return(nil) # quiet specs
    subject.handle
  end

end

