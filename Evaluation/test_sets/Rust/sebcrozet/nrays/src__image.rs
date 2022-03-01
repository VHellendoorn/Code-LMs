use std::io::Write;
use na::Vector3;
use na;
use math::Scalar;

use std::path::Path;
use na::Vector2;
use png;

pub type Vless = Vector2<Scalar>;

pub struct Image {
    extents: Vless, // extents of the rendering cube
    pixels:  Vec<Vector3<f32>>
}

impl Image {
    pub fn new(extents: Vless, pixels: Vec<Vector3<f32>>) -> Image {
        Image {
            extents: extents,
            pixels:  pixels
        }
    }
}

impl Image {
    pub fn to_ppm<W: Write>(&self, w: &mut W) {
        // XXX: there is something weird here…
        let width  = self.extents.x as usize;
        let height = self.extents.y as usize;

        let _ = w.write("P3\n".as_bytes());

        let _ = w.write(format!("{}", width).as_bytes());
        let _ = w.write(" ".as_bytes());
        let _ = w.write(format!("{}", height).as_bytes());
        let _ = w.write("\n".as_bytes());
        let _ = w.write("255\n".as_bytes());

        for i in 0 .. height {
            for j in 0 .. width {
                let c     = self.pixels[i * width + j];
                let color = c * 255.0f32;
                let white = Vector3::from_element(255.0);
                let valid_color = na::inf(&na::sup(&white, &color), &white);
                let px  = Vector3::new(valid_color.x as usize, valid_color.y as usize, valid_color.z as usize);

                let _ = w.write(format!("{}", px.x).as_bytes());
                let _ = w.write(" ".as_bytes());
                let _ = w.write(format!("{}", px.y).as_bytes());
                let _ = w.write(" ".as_bytes());
                let _ = w.write(format!("{}", px.z).as_bytes());
                let _ = w.write(" ".as_bytes());
            }

            let _ = w.write("\n".as_bytes());
        }
    }

    pub fn to_png(&self, path: &Path) {
        let width  = self.extents.x as usize;
        let height = self.extents.y as usize;

        let mut data: Vec<u8> = Vec::new();
        for i in 0 .. height {
            for j in 0 .. width {
                let c     = self.pixels[i * width + j].clone();
                let color = c * 255.0f32;
                let white = Vector3::from_element(255.0);
                let valid_color = na::inf(&na::sup(&color, &na::zero()), &white);
                let px = Vector3::new(valid_color.x as usize, valid_color.y as usize, valid_color.z as usize);

                data.push(px.x as u8);
                data.push(px.y as u8);
                data.push(px.z as u8);
            }
        }

        let mut img = png::Image {
            width:  width  as u32,
            height: height as u32,
            pixels: png::PixelsByColorType::RGB8(data)
        };

        let res = png::store_png(&mut img, path);

        if !res.is_ok() {
            panic!("Failed to save the output image.")
        }
    }
}
