require 'spec_helper'

class Driver
  include Brazenhead
end

describe Brazenhead do

  let(:driver) { Driver.new }
  let(:http_mock) { double("http_mock") }
  let(:http_response) { double("http_response").as_null_object }
  
  context "when calling a method directly on the module" do
    before(:each) do
      Net::HTTP.stub(:new).and_return(http_mock)
      http_mock.stub(:post).and_return(http_response)
      http_response.stub(:code).and_return('200')
    end
    
    it "should convert method name to camel case with first letter lowercase" do
      driver.should_receive(:call_method_on_driver).with("fooBar", [])
      driver.foo_bar
    end

    it "should make an http call passing the method name as the name" do
      Net::HTTP.should_receive(:new).and_return(http_mock)
      http_mock.should_receive(:post).with('/', "commands=[{\"name\":\"fooBar\"}]")
      driver.foo_bar
    end

    it "should make the result of the call available for inspection" do
      http_response.stub(:body).and_return("Success")
      http_mock.should_receive(:post).with('/', "commands=[{\"name\":\"fooBar\"}]")
      result = driver.foo_bar
      driver.last_response.should == result
    end

    it "should be able to return the json result" do
      http_response.stub(:body).and_return("false")
      driver.foo_bar
      driver.last_json.should be_false
    end
  end
end
