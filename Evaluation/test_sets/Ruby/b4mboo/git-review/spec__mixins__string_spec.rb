require 'spec_helper'

describe 'String helper' do

  subject { '2013-10-10T09:49:38Z' }

  context '#review_time' do

    it 'should return a formatted time' do
      subject.review_time.should eq '10-Oct-13'
    end

    it 'invalid time string should fail' do
      expect { 'foo'.review_time }.
        to raise_error(ArgumentError)
    end

  end

  context '#review_ljust' do

    subject { 'foo'.review_ljust(30) }

    it 'should have a fixed length' do
      subject.length.should eq 30
    end

    it 'should be filled with spaces' do
      subject.count(' ').should eq 27
    end

  end

end
