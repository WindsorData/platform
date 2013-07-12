class UploadLogController < ApplicationController
  before_filter {authorize!(:audit, :upload_log) }

  def audit
    @logs = UploadLog.limit(5).order('created_at desc')
  end
end
