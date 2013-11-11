class AddPeersAndTickersAndTypeToSearches < ActiveRecord::Migration
  def change
    add_column :searches, :peers, :text
    add_column :searches, :tickers, :text
    add_column :searches, :type, :string
  end
end
