# == Schema Information
#
# Table name: detail_upload_files
#
#  id            :integer          not null, primary key
#  file_name     :text
#  upload_log_id :integer
#  messages      :text
#  ticker        :string(255)
#

class DetailUploadFile < ActiveRecord::Base
  extend JSONLoadable

  attr_accessible :file_name, :messages, :ticker
  scope :containing_chars, lambda { |s| where("ticker ilike ?", "#{s}%") }

end
