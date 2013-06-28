require 'spec_helper'

describe Ticker do
  context "When getting tickers from backend" do
    it "Should add new tickers to the database" do
      count_before = Ticker.count
      json = [{ name: "GOOG" },{ name: "MMM" }].to_json

      Ticker.load_json(json)

      count_after = Ticker.count
      count_after.should == count_before + 2
    end
  end
end