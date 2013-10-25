module SearchHelper
  def log_search_path(search)
    case search.report_type
    when Constants::TOP5_REPORT
      recent_search_path(search)
    end
  end
end