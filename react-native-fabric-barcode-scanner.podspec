require "json"

package = JSON.parse(File.read(File.join(__dir__, "package.json")))

Pod::Spec.new do |s|
  s.name         = "react-native-fabric-barcode-scanner"
  s.module_name  = "RNCustomScanner"
  s.version      = package["version"]
  s.summary      = package["description"]
  s.homepage     = package["homepage"]
  s.license      = package["license"]
  s.authors      = package["author"]
  s.platforms    = { :ios => "13.4" }
  repo_url = package["repository"]["url"].sub(/^git\+/, "")
  s.source       = { :git => repo_url, :tag => "v#{s.version}" }
  s.static_framework = true

  s.source_files = "ios/**/*.{h,m,mm,swift}"

  s.dependency "React-Core"

  s.pod_target_xcconfig = {
    "DEFINES_MODULE" => "YES",
    "SWIFT_VERSION" => "5.0"
  }

  install_modules_dependencies(s)
end
