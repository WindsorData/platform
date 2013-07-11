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
