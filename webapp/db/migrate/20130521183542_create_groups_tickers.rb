class CreateGroupsTickers < ActiveRecord::Migration
  def self.up
    create_table :groups_tickers, :id => false do |t|
        t.references :group
        t.references :ticker
    end
    add_index :groups_tickers, [:group_id, :ticker_id]
    add_index :groups_tickers, [:ticker_id, :group_id]
  end

  def self.down
    drop_table :groups_tickers
  end
end
