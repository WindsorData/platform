class RemoveTickersFromGroups < ActiveRecord::Migration
  def up
    remove_column :groups, :tickers
  end

  def down
    add_column :groups, :tickers, :string
  end
end
