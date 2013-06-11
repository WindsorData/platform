require 'spec_helper'

describe Ticker do
  context "When getting tickers from backend" do
    it "Should add new tickers to the database" do
      count_before = Ticker.count
      json = [{"name"=>"uno"},{"name"=>"otro"}].to_json

      Ticker.load_tickers(json)

      count_after = Ticker.count
      count_after.should == count_before + 2
    end
  end
end