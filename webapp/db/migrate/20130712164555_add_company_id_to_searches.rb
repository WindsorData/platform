class AddCompanyIdToSearches < ActiveRecord::Migration
  def change
    add_column :searches, :company_id, :integer
  end
end
