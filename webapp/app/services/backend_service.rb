class BackendService
  def self.load_tickers
    url = Rails.application.config.backend_host + Rails.application.config.get_tickers_path
    json = RestClient.get url
    Ticker.load_json(json)
  end

  def self.post_file(type, file)
    case type
    when 'top5'
      path = Rails.application.config.post_top5_path
    when 'guidelines'
      path = Rails.application.config.post_guidelines_path
    when 'dilution'
      path = Rails.application.config.post_dilution_path
    when 'batch'
      path = Rails.application.config.post_batch_path
    end
    url = Rails.application.config.backend_host + path
    RestClient.post url, dataset: File.new(file.path, 'r')
  end

  def self.load_roles
    url = Rails.application.config.backend_host + Rails.application.config.get_roles_path
    json = RestClient.get url
    Role.load_json(json)
  end
end