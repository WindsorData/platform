class UploadLogController < ApplicationController
  def audit
    @logs = UploadLog.limit(5).order('created_at desc')
  end
end
