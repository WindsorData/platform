class AddTickerToTickers < ActiveRecord::Migration
  def change
    add_column :tickers, :ticker, :string
  end
end
