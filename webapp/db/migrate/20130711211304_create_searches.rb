class CreateSearches < ActiveRecord::Migration
  def change
    create_table :searches do |t|
      t.integer :user_id
      t.string :json_query

      t.timestamps
    end
  end
end
