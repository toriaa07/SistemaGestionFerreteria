using Microsoft.AspNetCore.Mvc;
using ferreteriaapi.Data;
using ferreteriaapi.Models;

namespace ferreteriaapi.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class ProductosController : ControllerBase
    {
        private readonly FerreteriaDbContext _context;

        public ProductosController(FerreteriaDbContext context)
        {
            _context = context;
        }

        [HttpGet]
        public IActionResult GetProductos()
        {
            var productos = _context.Productos.ToList();
            return Ok(productos);
        }

       [HttpGet("{id}")]
       public IActionResult GetProducto(int id)
       {
           var producto = _context.Productos.Find(id);
           if (producto == null)return NotFound();
           return Ok(producto);
        }

        [HttpPost]
        public IActionResult CreateProduct(Producto producto)
        {
            _context.Productos.Add(producto);
            _context.SaveChanges();
            return CreatedAtAction(nameof(GetProducto), new { id = producto.Id }, producto);
        }

        [HttpPut("{id}")]
        public IActionResult UpdateProducto(int id, Producto producto)
        {
            if (id != producto.Id)
            {
                return BadRequest();
            }

            var existingProducto = _context.Productos.Find(id);
            if (existingProducto == null)
            {
                return NotFound();
            }

            existingProducto.Nombre = producto.Nombre;
            existingProducto.Precio = producto.Precio;
            existingProducto.Descripcion = producto.Descripcion;
            existingProducto.Stock = producto.Stock;
            existingProducto.IdProveedor = producto.IdProveedor;

            _context.SaveChanges();
            return Ok(existingProducto);
        }

        [HttpPatch("{id}")]
        public IActionResult UpdateProductoPartial(int id, [FromBody] Producto producto)
        {
            var existingProducto = _context.Productos.Find(id);
            if (existingProducto == null) return NotFound();

            if (!string.IsNullOrEmpty(producto.Nombre))
                existingProducto.Nombre = producto.Nombre;

            if (producto.Precio > 0)
                existingProducto.Precio = producto.Precio;

            if (!string.IsNullOrEmpty(producto.Descripcion))
                existingProducto.Descripcion = producto.Descripcion;

            if (producto.Stock > 0)
                existingProducto.Stock = producto.Stock;

            if (producto.IdProveedor > 0)
                existingProducto.IdProveedor = producto.IdProveedor;

            _context.SaveChanges();
            return Ok(existingProducto);
        }

        [HttpDelete("{id}")]
        public IActionResult DeleteProducto(int id)
        {
            var producto = _context.Productos.Find(id);
            if (producto == null) return NotFound();

            _context.Productos.Remove(producto);
            _context.SaveChanges();
            return NoContent();
        }
    }
}
