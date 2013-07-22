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
  attr_accessible :upload_type, :message, :user
  belongs_to :user

  scope :with_user, lambda { |value| where('user_id = ?', value) if value }
  scope :created_since, lambda { |since| where('created_at >= ?', since) if since }
  scope :created_to, lambda { |to| where('created_at <= ?', to) if to }

  def results
    if message.blank?
      []
    else
      JSON.parse(message)['results'].to_a
    end
  end
end
