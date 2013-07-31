class CreateCompanyPeers < ActiveRecord::Migration
  def change
    create_table :company_peers do |t|
      t.string :ticker, null: false
      t.string :name

      t.timestamps
    end
  end
end
