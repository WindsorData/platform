# == Schema Information
#
# Table name: detail_upload_files
#
#  id            :integer          not null, primary key
#  file_name     :text
#  ticker_id     :integer
#  upload_log_id :integer
#  messages      :text
#

class DetailUploadFile < ActiveRecord::Base
  extend JSONLoadable

  attr_accessible :file_name, :messages, :ticker

end
