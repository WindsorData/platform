# == Schema Information
#
# Table name: upload_logs
#
#  id          :integer          not null, primary key
#  user        :string(255)
#  message     :text
#  upload_type :string(255)
#  created_at  :datetime         not null
#  updated_at  :datetime         not null
#

class UploadLog < ActiveRecord::Base
  extend JSONLoadable

  attr_accessible :upload_type, :user, :detail_upload_files
  belongs_to :user
  has_many :detail_upload_files

  scope :with_ticker, lambda { |value| joins(:detail_upload_files).where('ticker_id = ?', value) if value }
  scope :with_user, lambda { |value| where('user_id = ?', value) if value }
  scope :created_since, lambda { |since| where('created_at >= ?', since) if since }
  scope :created_to, lambda { |to| where('created_at <= ?', to) if to }

end
