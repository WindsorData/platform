class UploadLog < ActiveRecord::Base
  attr_accessible :upload_type, :message, :user
  belongs_to :user

  def self.notify_upload(upload)
    UploadLog.create(upload)
  end
end
