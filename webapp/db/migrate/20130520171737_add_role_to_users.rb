# bundle exec rails g migration add_role_to_users role:string
class AddRoleToUsers < ActiveRecord::Migration
  def change
    add_column :users, :role, :string
  end
end
