(ns knotlog.interface.repositories.file-storage-interface)

(defprotocol FileStorage
  "Protocol for file storage operations"
  (upload-file [this local-path remote-path] "Upload a file")
  (remove-file [this local-path remote-path] "Remove a file"))
