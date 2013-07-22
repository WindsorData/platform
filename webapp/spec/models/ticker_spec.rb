require 'spec_helper'

describe Ticker do
  context "When getting tickers from backend" do

    context "When there are multiples tickers to add" do
      it "Inserts them all in the database" do
        json = "[{\"cusip\":\"123ab\",\"ticker\":\"tkr1\",\"name\":\"Company 1\"}, {\"cusip\":\"456cd\",\"ticker\":\"tk2\",\"name\":\"Company 2\"}]"
        json_tickers_amount = JSON.parse(json).count

        expect { Ticker.load_json(json) }.to change(Ticker, :count).by(json_tickers_amount)
      end
    end

    context "When there is a ticker with the same cusip" do
      it "Updates the existing ticker with the new values" do
        create(:ticker, cusip: "123ab", ticker: "old_tkr", name: "old_name")
        json = "[{\"cusip\":\"123ab\",\"ticker\":\"new_tkr\",\"name\":\"new_name\"}]"
        values_hash = JSON.parse(json)
        existent_ticker = Ticker.find_by_cusip("123ab")
        
        existent_ticker.ticker.should eq "old_tkr"
        existent_ticker.name.should eq "old_name"

        Ticker.load_json(json)
        existent_ticker.reload 

        existent_ticker.ticker.should eq "new_tkr"
        existent_ticker.name.should eq "new_name"

      end
    end

    context "When there is a ticker with different cusip and the same ticker" do      
      it "Does not add the new ticker to the database" do
        create(:ticker, cusip: "123ab", ticker: "existent_tkr")
        json = "[{\"cusip\":\"456cd\",\"ticker\":\"existent_tkr\",\"name\":\"new_name\"}]"
        
        expect { Ticker.load_json(json) }.not_to change(Ticker, :count)
      end
    end

  end
end