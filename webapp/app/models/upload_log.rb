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

  def results
    if message.blank?
      []
    else
      JSON.parse(message)['results'].to_a
    end
  end
end
