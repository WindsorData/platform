class AddCusipToTickers < ActiveRecord::Migration
  def change
    add_column :tickers, :cusip, :string
  end
end
