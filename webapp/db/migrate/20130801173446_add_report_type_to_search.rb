class AddReportTypeToSearch < ActiveRecord::Migration
  def change
    add_column :searches, :report_type, :string
  end
end
