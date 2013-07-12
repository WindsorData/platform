class UploadLogController < ApplicationController
  before_filter {authorize!(:audit, :upload_log) }

  def audit
    @logs = UploadLog.order('created_at desc').paginated(params)
  end
end
