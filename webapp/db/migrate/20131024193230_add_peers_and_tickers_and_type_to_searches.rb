class AddPeersAndTickersAndTypeToSearches < ActiveRecord::Migration
  def change
    add_column :searches, :peers, :string
    add_column :searches, :tickers, :string
    add_column :searches, :type, :string
  end
end
