class AddCompanyIdToGroups < ActiveRecord::Migration
  def change
    add_column :groups, :company_id, :integer
  end
end
