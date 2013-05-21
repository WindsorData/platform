class AddTickersToGroups < ActiveRecord::Migration
  def change
    add_column :groups, :tickers, :string
  end
end
