$ErrorActionPreference = 'Stop'
Add-Type -AssemblyName System.Drawing
$source = Join-Path $PSScriptRoot '../docs/verification/native'
$output = Join-Path $PSScriptRoot 'gallery'
New-Item -ItemType Directory -Force $output | Out-Null
$encoder = [Drawing.Imaging.ImageCodecInfo]::GetImageEncoders() |
    Where-Object MimeType -eq 'image/jpeg'
$options = [Drawing.Imaging.EncoderParameters]::new(1)
$options.Param[0] = [Drawing.Imaging.EncoderParameter]::new(
    [Drawing.Imaging.Encoder]::Quality, [long]70
)
try {
    foreach ($file in Get-ChildItem $source -Filter '*.png') {
        $original = [Drawing.Image]::FromFile($file.FullName)
        try {
            $width = [Math]::Min(330, $original.Width)
            $height = [int]($original.Height * $width / $original.Width)
            $thumbnail = [Drawing.Bitmap]::new($original, $width, $height)
            try {
                $thumbnail.Save((Join-Path $output ($file.BaseName + '.jpg')), $encoder, $options)
            } finally { $thumbnail.Dispose() }
        } finally { $original.Dispose() }
    }
} finally { $options.Dispose() }
