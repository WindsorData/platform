class QueryGenerator

  def self.json_query(params_hash)
    a = {}
    a[:basics] = []
    params_hash.each_with_index { |(k1, v1), i1|
      if k1.starts_with? 'role'
        a[:basics][i1] = Hash[filters: [([])]]
        params_hash[k1].each_with_index { |(k2, v2), i2|
          if params_hash[k1][k2].is_a? Hash
            if params_hash[k1][k2]['type']
              if params_hash[k1][k2]['gt'] && params_hash[k1][k2]['lt']
                hash = Hash[field: k2 + "." + params_hash[k1][k2]['type'].downcase, operators: [( [Hash[gt: params_hash[k1][k2]['gt'], lt: params_hash[k1][k2]['lt']]] )] ]
              elsif params_hash[k1][k2]['gt']
                hash = Hash[field: k2 + "." + params_hash[k1][k2]['type'].downcase, operators: [( [Hash[gt: params_hash[k1][k2]['gt']]])]]
              elsif params_hash[k1][k2]['lt']
                hash = Hash[field: k2 + "." + params_hash[k1][k2]['type'].downcase, operators: [( [Hash[lt: params_hash[k1][k2]['lt']]])]]
              end
            else
              if params_hash[k1][k2]['gt'] && params_hash[k1][k2]['lt']
                hash = Hash[field: k2, operators: Array.new( [Hash[gt: params_hash[k1][k2]['gt'], lt: params_hash[k1][k2]['lt']]] ) ]
              elsif params_hash[k1][k2]['gt']
                hash = Hash[field: k2, operators: Array.new( [Hash[gt: params_hash[k1][k2]['gt']]])]
              elsif params_hash[k1][k2]['lt']
                hash = Hash[field: k2, operators: Array.new( [Hash[lt: params_hash[k1][k2]['lt']]])]
              end
            end     
          else
            hash = Hash[field: k2, value: params_hash[k1][k2]]
          end
          a[:basics][i1][:filters][i2] = hash
        }
      else # advanced search
        a[:advanced] = {} unless a[:advanced]
        a[:advanced][k1] = params_hash[k1]
      end
    }
    json_query = a.to_json.gsub!(/\"/, '\'').gsub(/'(\d)\'/,' \1')
  end
end