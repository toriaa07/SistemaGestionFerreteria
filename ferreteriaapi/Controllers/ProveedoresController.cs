using Microsoft.AspNetCore.Mvc;
using ferreteriaapi.Data;
using ferreteriaapi.Models;

namespace ferreteriaapi.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class ProveedoresController : ControllerBase
    {
        private readonly FerreteriaDbContext _context;

        public ProveedoresController(FerreteriaDbContext context)
        {
            _context = context;
        }

        [HttpGet]
        public IActionResult GetProveedores()
        {
            var proveedores = _context.Proveedores.ToList();
            return Ok(proveedores);
        }

        [HttpGet("{id}")]
        public IActionResult GetProveedor(int id)
        {
            var proveedor = _context.Proveedores.Find(id);
            if (proveedor == null) return NotFound();
            return Ok(proveedor);
        }

        [HttpPost]
        public IActionResult CreateProveedor(Proveedor proveedor)
        {
            _context.Proveedores.Add(proveedor);
            _context.SaveChanges();
            return CreatedAtAction(nameof(GetProveedor), new { id = proveedor.Id }, proveedor);
        }

        [HttpPut("{id}")]
        public IActionResult UpdateProveedor(int id, Proveedor proveedor)
        {
            if (id != proveedor.Id)
            {
                return BadRequest();
            }

            var existingProveedor = _context.Proveedores.Find(id);
            if (existingProveedor == null)
            {
                return NotFound();
            }

            existingProveedor.Nombre = proveedor.Nombre;
            existingProveedor.Telefono = proveedor.Telefono;
            existingProveedor.Email = proveedor.Email;

            _context.SaveChanges();
            return Ok(existingProveedor);
        }

        [HttpPatch("{id}")]
        public IActionResult UpdateProveedorPartial(int id, [FromBody] Proveedor proveedor)
        {
            var existingProveedor = _context.Proveedores.Find(id);
            if (existingProveedor == null) return NotFound();

            if (!string.IsNullOrEmpty(proveedor.Nombre))
                existingProveedor.Nombre = proveedor.Nombre;

            if (!string.IsNullOrEmpty(proveedor.Telefono))
                existingProveedor.Telefono = proveedor.Telefono;

            if (!string.IsNullOrEmpty(proveedor.Email))
                existingProveedor.Email = proveedor.Email;

            _context.SaveChanges();
            return Ok(existingProveedor);
        }

        [HttpDelete("{id}")]
        public IActionResult DeleteProveedor(int id)
        {
            var proveedor = _context.Proveedores.Find(id);
            if (proveedor == null) return NotFound();

            _context.Proveedores.Remove(proveedor);
            _context.SaveChanges();
            return NoContent();
        }
    }
}
