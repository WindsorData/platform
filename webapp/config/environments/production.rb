WindosorFrontend::Application.configure do
  # Settings specified here will take precedence over those in config/application.rb

  # Code is not reloaded between requests
  config.cache_classes = true

  # Full error reports are disabled and caching is turned on
  config.consider_all_requests_local       = false
  config.action_controller.perform_caching = true

  # Disable Rails's static asset server (Apache or nginx will already do this)
  config.serve_static_assets = true

  # Compress JavaScripts and CSS
  config.assets.compress = true

  # Don't fallback to assets pipeline if a precompiled asset is missed
  config.assets.compile = true

  # Generate digests for assets URLs
  config.assets.digest = true

  # Defaults to nil and saved in location specified by config.assets.prefix
  # config.assets.manifest = YOUR_PATH

  # Specifies the header that your server uses for sending files
  # config.action_dispatch.x_sendfile_header = "X-Sendfile" # for apache
  # config.action_dispatch.x_sendfile_header = 'X-Accel-Redirect' # for nginx

  # Force all access to the app over SSL, use Strict-Transport-Security, and use secure cookies.
  # config.force_ssl = true

  # See everything in the log (default is :info)
  # config.log_level = :debug

  # Prepend all log lines with the following tags
  # config.log_tags = [ :subdomain, :uuid ]

  # Use a different logger for distributed setups
  # config.logger = ActiveSupport::TaggedLogging.new(SyslogLogger.new)

  # Use a different cache store in production
  # config.cache_store = :mem_cache_store

  # Enable serving of images, stylesheets, and JavaScripts from an asset server
  # config.action_controller.asset_host = "http://assets.example.com"

  # Precompile additional assets (application.js, application.css, and all non-JS/CSS are already added)
  # config.assets.precompile += %w( search.js )
  # config.assets.precompile += %w( *.js *.css )

  # Disable delivery errors, bad email addresses will be ignored
  # config.action_mailer.raise_delivery_errors = false

  # Enable threaded mode
  # config.threadsafe!

  # Enable locale fallbacks for I18n (makes lookups for any locale fall back to
  # the I18n.default_locale when a translation can not be found)
  config.i18n.fallbacks = true

  # Send deprecation notices to registered listeners
  config.active_support.deprecation = :notify

  # Log the query plan for queries taking more than this (works
  # with SQLite, MySQL, and PostgreSQL)
  # config.active_record.auto_explain_threshold_in_seconds = 0.5

  # Only use best-standards-support built into browsers
  config.action_dispatch.best_standards_support = :builtin

  #SMTP
  MAILER_CREDENTIALS = HashWithIndifferentAccess.new(YAML.load(File.read(File.expand_path('config/mailer.yml'))))

  config.action_mailer.default_url_options = { :host => '54.203.246.78' }  
  config.action_mailer.delivery_method = :smtp
  config.action_mailer.smtp_settings = {
  :address              => "smtp.gmail.com",
  :port                 => 587,
  :user_name            => MAILER_CREDENTIALS[:email],
  :password             => MAILER_CREDENTIALS[:password],
  :authentication       => 'plain',
  :enable_starttls_auto => true  }

  # Backend params
  config.backend_host = 'http://localhost:9000'
  
  config.get_companies_path = '/api/companies'

  config.get_company_peers_path ='/api/companies/report/peers/tickers'
  config.post_incoming_peers_path = '/api/companies/report/peers/incoming'
  config.post_incoming_peers_raw_data_path = '/api/companies/report/peers/incoming/raw'
  config.post_peers_peers_single_ticker_path = '/api/companies/report/peers/peers'
  config.post_peers_peers_raw_data_path ="/api/companies/report/peers/peers/raw"
  config.post_peers_peers_ticker_list_raw_data_path ="/api/companies/report/peers/peersFromPrimary/raw"
  config.post_peers_peers_ticker_list_path = '/api/companies/report/peers/peersFromPrimary'

  # Get search form values
  config.get_primary_roles_path = '/api/schema/values/roles/primary'
  config.get_secondary_roles_path = '/api/schema/values/roles/secondary'  
  config.get_cash_compensations_path = '/api/schema/values/cashCompensations'
  config.get_equity_compensations_path = '/api/schema/values/equityCompensations'
  config.get_level_roles_path = '/api/schema/values/roles/level'
  config.get_scope_roles_path = '/api/schema/values/roles/scope'
  config.get_bod_roles_path = '/api/schema/values/roles/bod'
  
  # File upload paths
  config.post_top5_path = '/api/companies/top5'
  config.post_guidelines_path = '/api/companies/guidelines'
  config.post_dilution_path = '/api/companies/dilution'
  config.peers_path = '/api/companies/peers'
  config.drop_all_peers_path = '/api/companies/allPeers'
  config.post_bod_path = '/api/companies/bod'
  config.post_batch_companies_path = '/api/companies/batch/companies'
  config.post_batch_bod_path = '/api/companies/batch/bod'
  config.post_batch_peers_path = '/api/companies/batch/peers'
  config.post_company_index_path = '/api/companies/indexes'


  # Search paths
  config.post_query_path = '/api/companies/search'
  config.post_download_top5_report_path = '/api/companies/report'
  config.post_download_bod_report_path = '/api/companies/report/bod'
  config.post_download_full_report_path = '/api/companies/report/full'

end
