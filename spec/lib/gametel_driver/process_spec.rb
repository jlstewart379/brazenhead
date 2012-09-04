require 'spec_helper'

describe Process do
  let(:stdout) { double('stdout') }
  let(:stderr) { double('stderr') }
  let(:running_process) { double('process').as_null_object }
  let(:process) { GametelDriver::Process.new }

  before(:each) do
    Time.stub(:now) { 'this-time' }
    Tempfile.stub(:new).and_return(stdout,stderr)

    running_process.stub_chain(:io, :stdout).and_return(stdout)
    running_process.stub_chain(:io, :stdout=)
    running_process.stub_chain(:io, :stderr).and_return(stderr)
    running_process.stub_chain(:io, :stderr=)

    ChildProcess.stub(:build) { running_process }
  end

  it "it runs the command with arguments" do
    ChildProcess.should_receive(:build).with('command', 'some', 'argument')
    process.run('command', 'some', 'argument')
  end

  it "should wait for the command to complete" do
    running_process.should_receive(:wait)
    process.run('anything')
  end

  it "should redirect stdout and stderr" do
    Tempfile.should_receive(:new).with('gametel-proc-out-this-time')
    Tempfile.should_receive(:new).with('gametel-proc-err-this-time')
    process.run('anything')
  end

  it "should capture stdout and stderr" do
    running_process.io.should_receive(:stdout=).with(stdout)
    running_process.io.should_receive(:stderr=).with(stderr)
    process.run('anything')
  end

end
