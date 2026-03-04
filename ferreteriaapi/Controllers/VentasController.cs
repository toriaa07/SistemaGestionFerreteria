using Microsoft.AspNetCore.Mvc;
using ferreteriaapi.Data;
using ferreteriaapi.Models;

namespace ferreteriaapi.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class VentasController : ControllerBase
    {
        private readonly FerreteriaDbContext _context;

        public VentasController(FerreteriaDbContext context)
        {
            _context = context;
        }

        [HttpGet]
        public IActionResult GetVentas()
        {
            var ventas = _context.Ventas.ToList();
            return Ok(ventas);
        }

        [HttpGet("{id}")]
        public IActionResult GetVenta(int id)
        {
            var venta = _context.Ventas.Find(id);
            if (venta == null) return NotFound();
            return Ok(venta);
        }

        [HttpPost]
        public IActionResult CreateVenta(Venta venta)
        {
            _context.Ventas.Add(venta);
            _context.SaveChanges();
            return CreatedAtAction(nameof(GetVenta), new { id = venta.Id }, venta);
        }

        [HttpPut("{id}")]
        public IActionResult UpdateVenta(int id, Venta venta)
        {
            if (id != venta.Id)
            {
                return BadRequest();
            }

            var existingVenta = _context.Ventas.Find(id);
            if (existingVenta == null)
            {
                return NotFound();
            }

            existingVenta.Fecha = venta.Fecha;
            existingVenta.IdProducto = venta.IdProducto;
            existingVenta.CantidadVendida = venta.CantidadVendida;
            existingVenta.Total = venta.Total;

            _context.SaveChanges();
            return Ok(existingVenta);
        }

        [HttpPatch("{id}")]
        public IActionResult UpdateVentaPartial(int id, [FromBody] Venta venta)
        {
            var existingVenta = _context.Ventas.Find(id);
            if (existingVenta == null) return NotFound();

            if (venta.Fecha != default(DateOnly))
                existingVenta.Fecha = venta.Fecha;

            if (venta.IdProducto > 0)
                existingVenta.IdProducto = venta.IdProducto;

            if (venta.CantidadVendida > 0)
                existingVenta.CantidadVendida = venta.CantidadVendida;

            if (venta.Total > 0)
                existingVenta.Total = venta.Total;

            _context.SaveChanges();
            return Ok(existingVenta);
        }

        [HttpDelete("{id}")]
        public IActionResult DeleteVenta(int id)
        {
            var venta = _context.Ventas.Find(id);
            if (venta == null) return NotFound();

            _context.Ventas.Remove(venta);
            _context.SaveChanges();
            return NoContent();
        }
    }
}
