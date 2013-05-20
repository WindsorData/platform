# bundle exec rails g migration add_name_to_group name:string
class AddNameToGroup < ActiveRecord::Migration
  def change
    add_column :groups, :name, :string
  end
end
