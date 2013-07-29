class ChangeJsonQueryTypeInSearches < ActiveRecord::Migration
  def up
    change_column :searches, :json_query, :text
  end

  def down
    change_column :searches, :json_query, :string
  end
end
